/*package nl.rubend.clonebook.presentation.assembler;

import nl.rubend.clonebook.domain.Page;
import nl.rubend.clonebook.presentation.PageController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PageModelAssembler implements RepresentationModelAssembler<Page, EntityModel<Page>> {
	@Override
	public EntityModel<Page> toModel(Page page) {
		return EntityModel.of(page,
				linkTo(methodOn(PageController.class).one(page.getId())).withSelfRel(),
				linkTo(methodOn(PageController.class).all()).withRel("pages"));
	}
}
*/